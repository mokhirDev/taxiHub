package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.Expression;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.PageDto;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.VariableDto;
import com.mokhir.dev.telegram.bot.taxi.hub.dto.CallBackVariablesDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.Variable;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageBuilderService {
    private final LocalizationService localizationService;
    private final ButtonService buttonService;
    private final VariableService variableService;
    private final BotPageService botPageService;

    public SendMessage createNewMessage(UserState user, PageDto page) {
        Long chatId = user.getUserId();
        String text = generateLocaleText(user.getLocale(), page);
        Locale locale = getDefaultLang(user.getLocale());
        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text);

        InlineKeyboardMarkup inlineKeyboard = buttonService.buildInlineKeyboard(user, page.getButtons(), locale);
        ReplyKeyboardMarkup replyKeyboard = buttonService.buildReplyKeyboard(page.getButtons(), locale);
        InlineKeyboardMarkup dateInlineKeyboard = buttonService.buildDateInlineKeyboard(page.getButtons(), locale);

        if (inlineKeyboard != null && !inlineKeyboard.getKeyboard().isEmpty()) {
            builder.replyMarkup(inlineKeyboard);
        } else if (replyKeyboard != null && !replyKeyboard.getKeyboard().isEmpty()) {
            builder.replyMarkup(replyKeyboard);
        } else if (dateInlineKeyboard != null && !dateInlineKeyboard.getKeyboard().isEmpty()) {
            builder.replyMarkup(dateInlineKeyboard);
        }

        return builder.build();
    }


    public EditMessageText editMessage(UserState userState, PageDto nextPage) {
        Locale locale = Locale.of(userState.getLocale());

        String text = nextPage.getMessage().stream()
                .map(m -> localizationService.getMessage(m, locale))
                .collect(Collectors.joining("\n"));

        Long chatId = userState.getUserId();

        InlineKeyboardMarkup inlineKeyboardMarkup = buttonService.buildInlineKeyboard(userState, nextPage.getButtons(), locale);
        InlineKeyboardMarkup dateInlineKeyboardMarkup = buttonService.buildDateInlineKeyboard(nextPage.getButtons(), locale);

        EditMessageText.EditMessageTextBuilder builder = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(userState.getLastMessageId())
                .text(text);

        if (inlineKeyboardMarkup != null && !inlineKeyboardMarkup.getKeyboard().isEmpty()) {
            builder.replyMarkup(inlineKeyboardMarkup);
        } else if (dateInlineKeyboardMarkup != null && !dateInlineKeyboardMarkup.getKeyboard().isEmpty()) {
            builder.replyMarkup(dateInlineKeyboardMarkup);
        }
        return builder.build();
    }

    public DeleteMessage deleteMessage(UserState userState) {
        Long chatId = userState.getUserId();
        Integer messageId = userState.getLastMessageId();
        if (messageId == null || messageId == 0) {
            return null;
        }
        return DeleteMessage.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .build();
    }

    public void updateLastMessageId(Update update, UserState userState) {
        if (update.hasCallbackQuery()) {
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            if (userState.getLastMessageId() < messageId) {
                userState.setLastMessageId(messageId);
            }
        }
    }

    public Integer setLastMessageId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getMessageId();
        }
        return 0;
    }

    public EditMessageText calculateExpression(UserState userState, Update update, PageDto nextPage) {
        CallBackVariablesDto callBackVariablesDto = botPageService.getCallBackVariablesDto();
        Locale locale = Locale.of(userState.getLocale());
        String text = nextPage.getMessage().stream()
                .map(m -> localizationService.getMessage(m, locale))
                .collect(Collectors.joining("\n"));

        Expression expression = getExpression(update.getCallbackQuery().getData(), callBackVariablesDto);
        VariableDto variableDto = getVariable(expression, callBackVariablesDto);
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        EditMessageText.EditMessageTextBuilder builder = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text);

        Variable variable = variableService.getOrCreateVariable(userState, variableDto);
        Integer result = calculateExpression(variableDto, variable, expression.getAction());
        if (isAvailable(result, variableDto)) {
            variable.setVariableValue(result);
            variableService.saveVariable(variable);
        } else {
            return null;
        }


        InlineKeyboardMarkup inlineKeyboardMarkup = buttonService.buildInlineKeyboard(userState, nextPage.getButtons(), locale);
        if (inlineKeyboardMarkup != null && !inlineKeyboardMarkup.getKeyboard().isEmpty()) {
            builder.replyMarkup(inlineKeyboardMarkup);
        }

        return builder.build();
    }

    private VariableDto getVariable(Expression expression, CallBackVariablesDto callBackVariablesDto) {
        return callBackVariablesDto.getVariableDto().stream().filter(variableDto -> variableDto.getName().equals(expression.getVariableName())).findFirst().get();
    }

    public Expression getExpression(String callBack, CallBackVariablesDto callBackVariablesDto) {
        Optional<Expression> expression = callBackVariablesDto
                .getExpressionDto()
                .stream()
                .filter(exp -> exp.getName().equals(callBack))
                .findFirst();
        return expression.orElse(null);
    }

    Integer calculateExpression(VariableDto variableDto, Variable variable, String action) {
        String expression = variable.getVariableValue() + action;
        return SimpleCalc.eval(expression);
    }

    public Boolean isAvailable(Integer result, VariableDto variableDto) {
        return variableDto.getMax() >= result && result >= variableDto.getMin();
    }

    public String generateLocaleText(String localeText, PageDto page) {
        if (localeText == null || localeText.isEmpty()) {
            Locale localeUz = Locale.of("uz");
            Locale localeEn = Locale.of("en");
            Locale localeRu = Locale.of("ru");

            return page.getMessage()
                    .stream()
                    .map(mc -> {
                        String msgUz = localizationService.getMessage(mc, localeUz);
                        String msgRu = localizationService.getMessage(mc, localeRu);
                        String msgEn = localizationService.getMessage(mc, localeEn);
                        return msgUz + "\n" + msgRu + "\n" + msgEn;
                    })
                    .collect(Collectors.joining("\n"));
        }
        Locale locale = Locale.of(localeText);
        return page.getMessage().stream()
                .map(m -> localizationService.getMessage(m, locale))
                .collect(Collectors.joining("\n"));
    }

    private Locale getDefaultLang(String locale) {
        if (locale == null || locale.isEmpty()) {
            return Locale.of("en");
        }
        return Locale.of(locale);
    }

}
