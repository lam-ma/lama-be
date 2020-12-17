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


Player visits https://lama.kvarto.net/games/:id/player
Enters his name, sees the answers to current question,
 selects question, when host reveals the answer player sees the right answer
then next question
when game is finished he sees finish state


Player ws exchange:
1. server:  {"type": "login", "id": "ef73bbf"}
2. client: {"type": "join_game", "name": "Misha", "game_id": "blah"}
3. server:  {"type": "game_state", "game_id": "123", "title": "quizz title", "state": "QUESTION", "question": {...}, "right_answer_ids": ["blah"], "selected_answer_id": null }
4. client: {"type": "pick_answer", "question_id": "q1", "answer_id": "a1"}
                   
Host ws exchange:
1. server:  {"type": "login", "id": "ef73bbf"}
2. client: {"type": "create_game", "quizz_id": "123"}
3. server:  {"type": "game_state", ...} // same as for player
4. server:  {"type": "player_joined", "id": "blah", "name": "Leslye", "total_players": 10} 
