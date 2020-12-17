package integration

val quizzJson = """
    {
      "title": "New quizz",
      "questions": [
        {
          "id": "q1",
          "description": "Question #1",
          "answers": [
            {
              "id": "a1",
              "description": "Answer #1",
              "is_right": false
            },
            {
              "id": "a2",
              "description": "Answer #2",
              "is_right": true
            },
            {
              "id": "a3",
              "description": "Answer #3",
              "is_right": true
            },
            {
              "id": "a4",
              "description": "Answer #4",
              "is_right": true
            }
          ]
        }
      ]
    }

""".trimIndent()
