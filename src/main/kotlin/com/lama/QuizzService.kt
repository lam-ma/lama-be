package com.lama

import java.net.URL

interface QuizzService {
    fun get(id: QuizzId): Quizz?
}

class QuizzServiceImpl : QuizzService {
    override fun get(id: QuizzId): Quizz? {
        if (id == SAMPLE_QUIZZ.id) {
            return SAMPLE_QUIZZ
        }
        return null
    }
}


val SAMPLE_QUIZZ = Quizz(
    QuizzId("123"),
    "Some title",
    listOf(
        Question(
            QuestionId("qid_1"),
            "Who is EM of MKP-2?",
            URL("https://www.jochen-schweizer.de/on/demandware.static/-/Sites-Class/default/dwdf47d577/products/extragross/lama-trekking-1.jpg"),
            listOf(
                Answer(AnswerId("aid_11"), "Aviv Sharked", true),
                Answer(AnswerId("aid_12"), "Marta", false),
                Answer(AnswerId("aid_13"), "Misha", false),
                Answer(AnswerId("aid_14"), "Lukasz", false),
            )
        ),
        Question(
            QuestionId("qid_2"),
            "What about order?",
            null,
            listOf(
                Answer(AnswerId("aid_21"), "Right answer #2", true),
                Answer(AnswerId("aid_22"), "BLah", true),
                Answer(AnswerId("aid_23"), "Some other answer", false),
                Answer(AnswerId("aid_24"), "IDK random", false),
            )
        ),
    )
)
