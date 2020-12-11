Queez host page:
Purely FE url:
https://lama.kvarto.net/quizzes/:id/host

BE:
GET /quizzes/:id 
 returns 200 full quize json

When user presses "Start" button FE makes call to

POST /quizzes/:id/start
returns 201 json structure with game_id


Game (=quizz session) id:
Purely FE url:
https://lama.kvarto.net/games/:id/host

BE:
GET /games/:id
 returns 200 full quizz json (including quizz id),
             current_question_id?, 
             game state=QUESTION|ANSWER|FINISH

GET /games/:id/scores?limit=10 
 returns 200 {scores: [ {name: "player_1", "score": 122}, ... ] }

POST /games/:id  body: {"question_id": "...", "state": "QUESTION" }  



