# sco.climb.wsn-event-store
----------
## REST API methods
----------
### Header
  - **Content-Type** = 'application/json'
  - **X-ACCESS-TOKEN** = 'token'

### Event Search 
```
  GET /api/event/{ownerId}
```

#### Params
  - **routeId**: string, optional
  - **dateFrom**: string, mandatory, "yyyy-MM-dd'T'HH:mm:ss"
  - **dateTo**: string, mandatory, "yyyy-MM-dd'T'HH:mm:ss"
  - **eventType[]**: string, mulitple value 
  - **nodeId[]**: string, multiple value

#### Result
    [
    {
      "ownerId": "TEST",
      "creationDate": 1453815205305,
      "lastUpdate": 1453815205305,
      "routeId": "ROUTE66",
      "wsnNodeId": "id1",
      "eventType": 101,
      "timestamp": 1453815205136,
      "payload": {
        "state": 1
      }
    },
    {...}
    ]

### Add Event 
```
  POST /api/event/{ownerId}
```

#### Boby
    [
    {
      "ownerId": "TEST",
      "creationDate": 1453815205305,
      "lastUpdate": 1453815205305,
      "routeId": "ROUTE66",
      "wsnNodeId": "id1",
      "eventType": 101,
      "timestamp": 1453815205136,
      "payload": {
        "state": 1
      }
    },
    {...}
    ]
    
### Upload Logs
```
  POST /api/log/upload/{ownerId}
```

#### Params
  - **name**: filename - string, mandatory
  - **file**: file, mandatory
