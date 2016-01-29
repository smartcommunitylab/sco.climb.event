# sco.climb.wsn-event-store
----------
## REST API methods
----------
### Event Search 
```
  GET /api/event/{ownerId}
```

#### Header
  - **Content-Type** = 'application/json'
  - **X-ACCESS-TOKEN** = 'token'

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
      "wsnNodeId": 1,
      "eventType": 101,
      "timestamp": 1453815205136,
      "payload": {
        "state": 1
      }
    },
    {...}
    ]
