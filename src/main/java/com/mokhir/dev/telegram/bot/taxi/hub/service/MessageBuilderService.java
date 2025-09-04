package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.*;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.Variable;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.AnswerTypeEnum;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.LocaleEnum;
import com.mokhir.dev.telegram.bot.taxi.hub.util.TemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageBuilderService {
    private final DynamicSqlExecutor executor;
    private final ButtonService buttonService;
    private final BotPageService botPageService;
    private final VariableService variableService;
    private final QueryLoadService queryLoadService;
    private final LocalizationService localizationService;

    public SendMessage createNewMessage(UserState user, PageDto page) {
        Long chatId = user.getUserId();
        String text = generateLocaleText(user.getLocale().toString(), page);
        Locale locale = getDefaultLang(user.getLocale().toString());
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


    public EditMessageText editMessage(UserState userState, PageDto nextPage, Update update) {
        Locale locale = Locale.of(userState.getLocale().toString());
        String text = buildMessage(nextPage, update, locale);

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
        Locale locale = Locale.of(userState.getLocale().toString());
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
        if (localeText.equals(LocaleEnum.UNKNOWN.toString()) || localeText.isEmpty()) {
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
        if (locale == null || locale.isEmpty() || locale.equals(LocaleEnum.UNKNOWN.toString())) {
            return Locale.of("en");
        }
        return Locale.of(locale);
    }

    @SneakyThrows
    public List<Object> executeQueries(Update update, PageDto currentPage) {
        List<Object> results = new ArrayList<>();
        AnswerTypeEnum answerType = botPageService.getAnswerType(update);

        for (String query : currentPage.getQuery()) {
            QueryConfigDto queryConfigDto = queryLoadService.getQueryByName(query);
            if (queryConfigDto != null && (queryConfigDto.getAnswer().getType() != null
                    || queryConfigDto.getAnswer().getExpectedAnswers() != null)) {

                AnswerDto answer = queryConfigDto.getAnswer();
                assert answer.getType() != null;

                boolean isValidAnswer = answer.getType().contains(answerType.toString()) &&
                        (answer.getExpectedAnswers() != null &&
                                (answer.getExpectedAnswers().contains("*") ||
                                        answer.getExpectedAnswers().contains(botPageService.getUpdateText(update))));

                if (isValidAnswer) {
                    results.add(executor.execute(queryConfigDto, update));
                }
            } else {
                assert queryConfigDto != null;
                results.add(executor.execute(queryConfigDto, update));
            }
        }
        return results;
    }

    public String buildMessage(PageDto nextPage, Update update, Locale locale) {
        String text = nextPage.getMessage().stream()
                .map(m -> localizationService.getMessage(m, locale))
                .collect(Collectors.joining("\n"));
        List<Object> object = executeQueries(update, nextPage);
        return TemplateUtil.fillTemplate(text, object);
    }
}
