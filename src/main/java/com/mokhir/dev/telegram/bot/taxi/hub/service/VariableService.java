package com.mokhir.dev.telegram.bot.taxi.hub.service;

import com.mokhir.dev.telegram.bot.taxi.hub.dto.VariableDto;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.Variable;
import com.mokhir.dev.telegram.bot.taxi.hub.repository.VariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VariableService {
    private final VariableRepository variableRepository;

    public Variable getOrCreateVariable(UserState userState, VariableDto variableDto) {
        Variable variables = variableRepository.findVariables(userState.getUserId(), userState.getLastMessageId(), variableDto.getName());
        if (variables == null) {
            variables = Variable
                    .builder()
                    .chatId(userState.getUserId())
                    .messageId(userState.getLastMessageId())
                    .variableName(variableDto.getName())
                    .variableValue(variableDto.getValue())
                    .build();
            variableRepository.save(variables);
            return variables;
        } else {
            return variables;
        }
    }

    public Variable saveVariable(Variable variable) {
        return variableRepository.saveAndFlush(variable);
    }
}
