package com.mokhir.dev.telegram.bot.taxi.hub.repository;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.Variable;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VariableRepository extends JpaRepository<Variable, Long> {

    @Query(value = """
            select * from variables where chat_id=:chatId and message_id = :messageId and variable_name = :variableName
            """, nativeQuery = true)
    Variable findVariables(@Param("chatId") Long chatId, @Param("messageId") Integer messageId, @Param("variableName") String variableName);

}
