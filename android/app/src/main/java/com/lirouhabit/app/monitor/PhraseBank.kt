package com.lirouhabit.app.monitor

/**
 * Frases de intervenção — reflexivas, feitas para TOCAR NA FERIDA.
 * Nunca clichês de autoajuda ("seu tempo esgotou" é proibido aqui).
 *
 * Placeholders: {sonho}, {vicio}, {app}, {tempo}, {hobby}, {hobby_tempo}
 * O sorteio evita repetir a última frase do mesmo nível.
 */
object PhraseBank {

    /** Nível 1 — limite de sessão: cutucada reflexiva. */
    private val level1 = listOf(
        "{tempo} minutos no {app}. Parece pouco. É assim que dias inteiros viram meses perdidos.",
        "Você não está descansando. Está fugindo. E \"{sonho}\" está pagando a conta.",
        "Cada minuto aqui é um \"não\" silencioso para \"{sonho}\".",
        "Ninguém perde para \"{vicio}\" de uma vez. Perde {tempo} minutos de cada vez.",
        "O {app} lucra com cada segundo seu. \"{sonho}\" só tem você.",
        "Daqui a pouco faz 15 minutos. E amanhã você nem lembra do que viu aqui.",
        "Seu hobby ({hobby}) está {hobby_tempo}. O {app} levou parte disso agora.",
        "Você abriu por um segundo e já são {tempo} minutos. Quem decidiu isso foi você ou o algoritmo?",
        "O vídeo pode esperar. O trecho de {hobby} que você adiou não ficou mais fácil.",
        "Antes do próximo vídeo: o que você escolheria fazer se o {app} sumisse do celular hoje?"
    )

    /** Nível 2 — 15 min acumulados no dia: comparação que dói. */
    private val level2 = listOf(
        "15 minutos hoje. Alguém com a sua mesma rotina usou esses 15 minutos — e saiu na frente.",
        "Alguém que ficou 15 minutos a menos no {app} hoje está mais perto de \"{sonho}\" do que você.",
        "\"{vicio}\" não vence com força. Vence com repetição. E você somou esses 15 minutos em idas e vindas.",
        "Se esses 15 minutos tivessem ido para \"{sonho}\", algo teria mudado. Viraram nada.",
        "Pergunta honesta: o que você ganhou nesses 15 minutos? E o que \"{sonho}\" perdeu?",
        "O algoritmo do {app} foi desenhado por gênios para te segurar. Sair agora é provar que você é mais forte.",
        "{hobby} está {hobby_tempo}. Quinze minutos aqui não parecem ser o motivo. Mas já são 15 provas.",
        "Não foi uma sessão longa: foram várias pequenas. Seu dia foi picotado até sobrar isso.",
        "Você pode chamar de pausa. Mas {hobby} também pede pausa do que te cansa — e está {hobby_tempo}.",
        "Se o {app} desse dinheiro por esses 15 minutos, você aceitaria a troca? Ele não dá."
    )

    /** Nível 3 — primeira chegada a 1 hora no dia: ferida aberta. */
    private val level3 = listOf(
        "1 hora aqui. Não foi tempo que passou — foi a parte de hoje que \"{sonho}\" não recebeu.",
        "1 hora por dia no {app} = 15 dias inteiros por ano. Imagine {hobby} com 15 dias a mais.",
        "Daqui a uma semana você não lembra de nada desta hora. Mas {hobby} vai continuar {hobby_tempo}.",
        "Você passou 1 hora vendo a vida dos outros. \"{sonho}\" é a sua. Ela está em pausa.",
        "Essa hora não volta. E amanhã o {app} vai pedir outra. Quem decide é você — agora.",
        "Ninguém no fim da vida diz \"queria mais {app}\". Muitos dizem \"queria ter feito mais {hobby}\".",
        "Vale mesmo a pena? Uma hora da sua vida, e a parte mais importante de você continua esperando.",
        "Você já cruzou a 1 hora hoje. Da próxima vez que voltar ao {app}, responda: o que exatamente ainda falta ver?"
    )

    /** Nível 4 — depois da 1h, cada NOVA entrada no vilão: confronto direto. */
    private val reentry = listOf(
        "Vale mesmo a pena? Você já passou {tempo} minutos no {app} hoje — e escolheu voltar.",
        "Você saiu e voltou. Não foi esquecimento: foi decisão. Vale mesmo a pena trocar \"{sonho}\" por mais esse scroll?",
        "Que vídeo, post ou curtida valeria mais uma parte do seu dia? Vale mesmo a pena?",
        "Seu hobby ({hobby}) está {hobby_tempo}. O {app} não precisa de mais atenção. Ele precisa?",
        "Você já teve 1 hora inteira de aviso hoje. O que mudou nos últimos minutos para precisar voltar?",
        "\"Só mais uma olhada\" já virou {tempo} minutos hoje. Vale mesmo a pena repetir a frase?",
        "O {app} sempre terá outro vídeo. Seu dia não terá outra versão. Vale mesmo a pena?",
        "Não vou pedir disciplina. Só honestidade: se um amigo visse essa nova entrada, o que você diria a ele?"
    )

    private val lastPicks = mutableMapOf<String, String>()

    @Synchronized
    private fun pick(key: String, pool: List<String>): String {
        val candidates = if (pool.size > 1) pool.filter { it != lastPicks[key] } else pool
        return candidates.random().also { lastPicks[key] = it }
    }

    fun phrase(
        level: Int,
        dream: String,
        vice: String,
        app: String,
        minutes: Int,
        hobby: String = "",
        hobbyLastAt: Long = 0L
    ): String {
        val (key, pool) = when {
            level >= 4 -> "reentry" to reentry
            level == 3 -> "level3" to level3
            level == 2 -> "level2" to level2
            else -> "level1" to level1
        }
        val hobbyName = hobby.ifBlank { "o que você ama fazer fora das telas" }
        return pick(key, pool)
            .replace("{sonho}", dream.ifBlank { "seu maior sonho" })
            .replace("{vicio}", vice.ifBlank { "a procrastinação" })
            .replace("{app}", app)
            .replace("{tempo}", minutes.toString())
            .replace("{hobby}", hobbyName)
            .replace("{hobby_tempo}", idleText(hobbyLastAt))
    }

    /** Texto humano: "há 3 dias sem sua atenção", "há 5 horas sem sua atenção". */
    fun idleText(lastPracticedAt: Long, now: Long = System.currentTimeMillis()): String {
        if (lastPracticedAt <= 0L) return "ainda sem um registro de prática"
        val hours = ((now - lastPracticedAt).coerceAtLeast(0L) / 3_600_000L).toInt()
        val days = hours / 24
        return when {
            days >= 60 -> "há ${days / 30} meses sem sua atenção"
            days >= 30 -> "há 1 mês sem sua atenção"
            days >= 14 -> "há ${days / 7} semanas sem sua atenção"
            days >= 2 -> "há $days dias sem sua atenção"
            days == 1 -> "há 1 dia sem sua atenção"
            hours >= 2 -> "há $hours horas sem sua atenção"
            else -> "sem atenção desde a última prática"
        }
    }
}
