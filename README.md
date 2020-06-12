# Sicredi - votação em pautas

O projeto foi desenvolvido como parte de um teste para back-end no Sicredi.
A documentação para integração com o sistema está disponível no  [Swagger](https://sicredi-mateus.herokuapp.com/swagger-ui.html)


## Métodos

| HTTP | Descrição |
|---|---|
| `GET` | Busca as informações sobre as pautas. |
| `POST` | Cria um novo registro de pauta e votação. |
| `PUT` | Atualiza dados de uma pauta. |

## Respostas

| Código | Descrição |
|---|---|
| `200` | Sucesso. (Success)|
| `400` | Erro na validação do objeto enviado à API. (Bad Request)|
| `404` | ID informado não foi encontrado. (Not Found)|
| `409` | Conflito, o registro já existe. (Conflict)|


## Recursos disponíveis via api:

URL base: [/v1/guideline](https://sicredi-mateus.herokuapp.com/v1/guideline)

### Listar todas as pautas [GET]
Retorna todas as pautas cadastradas.

+ Request (application/json)
    + Query params: 
        page, size
        
+ Response 200 (application/json)
        
        [
          {
            "description": "string",
            "id": "string",
            "name": "string"
          },
          {
            "description": "string",
            "id": "string",
            "name": "string"
          }
        ]

### Stream todas as pautas [GET (/stream)]
Retorna um stream de pautas.

+ Request (application/json)
        
+ Response 200 (application/json)
        
        [
          {
            "description": "string",
            "id": "string",
            "name": "string"
          }
        ]

### Buscar pauta por id [GET /{id}]
Retorna uma pauta por id.

+ Request (application/json)
    
        
+ Response 200 (application/json)
        
        {
            "description": "string",
            "id": "string",
            "name": "string"
        }
        
+ Response 404 (application/json)
        
        {
          "timestamp": Long,
          "path": "/v1/guideline/{id}",
          "status": 404,
          "error": "Not Found",
          "message": "Id não encontrado",
          "requestId": string
        }

        
### Criar uma pauta [POST]
Criar uma pauta.

+ Request (application/json)
        
        {
            "name": "exemplo",
            "description": "exemplo"
        }
        
+ Response 200 (application/json)
        
        {
            "id": "string",
            "name": "string",
            "description": "string",
        }

+ Response 409 (application/json)
        
        {
            "timestamp": long,
            "path": "/v1/guideline",
            "status": 409,
            "error": "Conflict",
            "message": "Pauta já registrada",
            "requestId": string
        }
        
### Atualizar pauta [PUT /{id}]
Atualizar dados de uma pauta por id.

+ Request (application/json)
        
        {
            "name": "exemplo",
            "description": "exemplo"
        }
        
+ Response 200 (application/json)
        
        {
            "id": "string",
            "name": "string",
            "description": "string",
        }
        
+ Response 404 (application/json)
        
        {
          "timestamp": Long,
          "path": "/v1/guideline/{id}",
          "status": 404,
          "error": "Not Found",
          "message": "Id não encontrado",
          "requestId": string
        }


### Abrir sessão  [POST /{id}/open]
Abrir sessão para uma pauta, buscando a pauta por id.
Necessário enviar a duração da sessão em minutos, caso contrário a duração padrão será de um minuto.

+ Request (application/json)
        
        {
            "duration": 120
        }
        
+ Response 200 (application/json)
            
        {
          "associates": [
            {
              "cpf": "string"
            }
          ],
          "description": "string",
          "name": "string",
          "negative": 0,
          "positive": 0,
          "votingEnd": "2020-06-12T17:52:11.061Z",
          "votingStart": "2020-06-12T17:52:11.061Z"
        }
        
+ Response 404 (application/json)
        
        {
          "timestamp": Long,
          "path": "/v1/guideline/{id}/open",
          "status": 404,
          "error": "Not Found",
          "message": "Id não encontrado",
          "requestId": string
        }

+ Response 409 (application/json)
        
        {
          "timestamp": Long,
          "path": "/v1/guideline/{id}/open",
          "status": 409,
          "error": "Conflict",
          "message": "Sessão já iniciada",
          "requestId": string
        }
        
### Enviar voto  [POST /{id}/vote]
Enviar voto de uma pauta por id da pauta.

+ Request (application/json)
        
        {
           "cpf": "string",
           "decision": true   
        }
+ Response 200

+ Response 409 (application/json)
        
        {
          "timestamp": Long,
          "path": "/v1/guideline/{id}/vote",
          "status": 409,
          "error": "Conflict",
          "message": "Associado já votou nessa pauta"",
          "requestId": string
        }
        
+ Response 400 (application/json)
        
        {
          "timestamp": Long,
          "path": "/v1/guideline/{id}/vote",
          "status": 400,
          "error": "Bad Request",
          "message": "Associado impedido de votar",,
          "requestId": string
        }

+ Response 404 (application/json)
        
        {
          "timestamp": Long,
          "path": "/v1/guideline/{id}/vote",
          "status": 404,
          "error": "Not Found",
          "message": "Id não encontrado",
          "requestId": string
        }


### Buscar dados da sessão  [GET /{id}/session]
Buscar dados de votação da pauta por id.

+ Request (application/json)
        
+ Response 200 (application/json)
            
        {
          "associates": [
            {
              "cpf": "string"
            }
          ],
          "description": "string",
          "name": "string",
          "negative": 0,
          "positive": 0,
          "votingEnd": "2020-06-12T17:52:11.061Z",
          "votingStart": "2020-06-12T17:52:11.061Z"
        }    
            
+ Response 404 (application/json)
        
        {
          "timestamp": Long,
          "path": "/v1/guideline/{id}/session",
          "status": 404,
          "error": "Not Found",
          "message": "Id não encontrado",
          "requestId": string
        }
