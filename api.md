Queez host page:
Purely FE url:
/queezes/:id/host
^
BE:
GET /queezes/:id 
 returns 200 full queeze json
When user presses "Start" button FE makes call to

POST /queezes/:id/start
returns 201 json structure with game_id


Queez session (=game) id:
Purely FE url:
/games/:id/host

BE:
GET /games/:id
 returns 200 full queeze json (including queez_id),
             current_question_id?, 
             game state=QUESTION|ANSWER|FINISH

GET /games/:id/scores?limit=10 
 returns 200 {scores: [ {name: "player_1", "score": 122}, ... ] }

POST /games/:id  body: {"question_id": "...", "state": "QUESTION" }  



