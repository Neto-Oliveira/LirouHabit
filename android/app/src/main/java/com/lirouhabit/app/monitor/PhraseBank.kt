package com.lirouhabit.app.monitor

/**
 * Frases de intervenção — reflexivas, feitas para TOCAR NA FERIDA.
 * Nunca clichês de autoajuda ("seu tempo esgotou" é proibido aqui).
 *
 * Placeholders: {sonho}, {vicio}, {app}, {tempo}
 */
object PhraseBank {

    /** Nível 1 — limite do app (default 5 min): cutucada reflexiva. */
    private val level1 = listOf(
        "{tempo} minutos no {app}. Parece pouco. É assim que dias inteiros viram meses perdidos.",
        "Você não está descansando. Está fugindo. E \"{sonho}\" está pagando a conta.",
        "Cada minuto aqui é um \"não\" silencioso para \"{sonho}\".",
        "Ninguém perde para \"{vicio}\" de uma vez. Perde {tempo} minutos de cada vez.",
        "O {app} lucra com cada segundo seu. \"{sonho}\" só tem você.",
        "Daqui a pouco faz 15 minutos. E amanhã você nem lembra do que viu aqui."
    )

    /** Nível 2 — 15 min: comparação que dói. */
    private val level2 = listOf(
        "15 minutos. Alguém com a sua mesma rotina usou esses 15 minutos hoje — e saiu na frente.",
        "Alguém que ficou 15 minutos a menos no {app} hoje está mais perto de \"{sonho}\" do que você.",
        "\"{vicio}\" não vence com força. Vence com repetição. Você está repetindo agora.",
        "Se esses 15 minutos tivessem ido para \"{sonho}\", algo teria mudado. Viraram nada.",
        "Pergunta honesta: o que você ganhou nesses 15 minutos? E o que \"{sonho}\" perdeu?",
        "O algoritmo do {app} foi desenhado por gênios para te segurar. Sair agora é provar que você é mais forte."
    )

    /** Nível 3 — 1 hora: tela de bloqueio. A ferida aberta. */
    private val level3 = listOf(
        "1 hora aqui. Não foi tempo que passou — foi a parte de hoje que \"{sonho}\" não recebeu.",
        "1 hora por dia no {app} = 15 dias inteiros por ano. Imagine o que \"{sonho}\" seria com 15 dias a mais.",
        "Daqui a uma semana você não lembra de nada que viu nesta hora. Mas vai lembrar de não ter chegado em \"{sonho}\".",
        "Você passou 1 hora vendo a vida dos outros. \"{sonho}\" é a sua. Ela está em pausa.",
        "Essa hora não volta. E amanhã o {app} vai pedir outra. Quem decide é você — agora.",
        "Ninguém no fim da vida diz \"queria ter passado mais tempo no {app}\". Muitos dizem \"queria ter corrido atrás de {sonho}\"."
    )

    fun phrase(level: Int, dream: String, vice: String, app: String, minutes: Int): String {
        val pool = when {
            level <= 1 -> level1
            level == 2 -> level2
            else -> level3
        }
        return pool.random()
            .replace("{sonho}", dream.ifBlank { "seu maior sonho" })
            .replace("{vicio}", vice.ifBlank { "a procrastinação" })
            .replace("{app}", app)
            .replace("{tempo}", minutes.toString())
    }
}
