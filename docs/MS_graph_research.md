# MS graph lib research
Investigation into the ms graph to ensure we're creating the right interface to the APIs.

Using this graph explorer: https://developer.microsoft.com/graph/graph-explorer

## Objects
### Todo
Properties
* id
* status
* title
* body
* due
* status
### Event
Taken from (search API)[#calendar-search] as a list of this object inside of top-level "value" property.
Properties
* id
* subject
* body -> {body: content}
* link -> webLink
* start -> {start: convertTime(dateTime, timeZone)}
* end -> {end: convertTime(dateTime, timeZone)}
* organizer -> {organizer: {emailAddress: address}} (?)

## APIs
### Todo list
Permissions: Tasks.ReadWrite
#### Todo see lists
GET https://graph.microsoft.com/v1.0/me/todo/lists
Response:
```
{
    "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#users('[redacted]')/todo/lists",
    "value": [
        {
            "@odata.etag": "[redacted]",
            "displayName": "Tasks",
            "isOwner": true,
            "isShared": false,
            "wellknownListName": "defaultList",
            "id": "AAMkAGVmMDEzMTM4LTZmYWUtNDdkNC1hMDZiLTU1OGY5OTZhYmY4OAAuAAAAAAAiQ8W967B7TKBjgx9rVEURAQAiIsqMbYjsT5e-T7KzowPTAAAAAAESAAA="
        }
    ]
}
```

Then get tasks in a list:
GET /me/todo/lists/{todoTaskListId}/tasks
Response:
```
{
   "value":[
      {
         "@odata.etag":"W/\"xzyPKP0BiUGgld+lMKXwbQAAgdhkVw==\"",
         "importance":"low",
         "isReminderOn":false,
         "status":"notStarted",
         "title":"Linked entity new task 1",
         "createdDateTime":"2020-07-08T11:15:19.9359889Z",
         "lastModifiedDateTime":"2020-07-08T11:15:20.0614375Z",
         "id":"AQMkADAwATM0MDAAMS0yMDkyLWVjMzYtMDACLTAwCgBGAAAD",
         "body":{
            "content":"",
            "contentType":"text"
         },
         "linkedResources@odata.context":"https://graph.microsoft.com/beta/$metadata#users('todoservicetest2412201901%40outlook.com')/todo/lists('35e2-35e2-721a-e235-1a72e2351a7')/tasks('AQMkADAwATM0MDAAMS0yMDkyLWVjMzYtMDACLTAwCgBGAAAD')/linkedResources",
         "linkedResources":[
            {
               "applicationName":"Partner App Name",
               "displayName":"Partner App Name",
               "externalId":"teset1243434",
               "id":"30911960-7321-4cba-9ba0-cdb68e2984c7"
            }
         ]
      }
   ]
}
```

### Calendar
Permission: Calendars.ReadWrite
#### Calendar search
GET https://graph.microsoft.com/v1.0/me/calendarview?startdatetime=2021-05-07T01:29:28.025Z&enddatetime=2021-05-14T01:29:28.025Z

```json
{
    "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#users('pierre%40larochelle.io')/calendarView",
    "value": [
        {
            "@odata.etag": "W/\"khSEU72QA0mRg8Uq8xmwuAACnc0mcg==\"",
            "id": "AQMkADAwATM0MDAAMS0xMDE3LWZjMDgtMDACLTAwCgBGAAADRm2j_Ij4P0eT30FgtOJmbAcAkhSEU72QA0mRg8Uq8xmwuAAAAgENAAAAkhSEU72QA0mRg8Uq8xmwuAACndCj8wAAAA==",
            "createdDateTime": "2021-05-07T01:30:04.3182486Z",
            "lastModifiedDateTime": "2021-05-07T01:30:06.9526931Z",
            "changeKey": "khSEU72QA0mRg8Uq8xmwuAACnc0mcg==",
            "categories": [],
            "transactionId": "a3908c8b-0a1a-e0e7-30ad-4c35d9617c4d",
            "originalStartTimeZone": "Pacific Standard Time",
            "originalEndTimeZone": "Pacific Standard Time",
            "iCalUId": "040000008200E00074C5B7101A82E0080000000081AC7B81E042D701000000000000000010000000D59A250A59BF624392748C31F0800437",
            "reminderMinutesBeforeStart": 15,
            "isReminderOn": true,
            "hasAttachments": false,
            "subject": "Future? task",
            "bodyPreview": "",
            "importance": "normal",
            "sensitivity": "normal",
            "isAllDay": false,
            "isCancelled": false,
            "isOrganizer": true,
            "responseRequested": true,
            "seriesMasterId": null,
            "showAs": "busy",
            "type": "singleInstance",
            "webLink": "https://outlook.live.com/owa/?itemid=AQMkADAwATM0MDAAMS0xMDE3LWZjMDgtMDACLTAwCgBGAAADRm2j%2BIj4P0eT30FgtOJmbAcAkhSEU72QA0mRg8Uq8xmwuAAAAgENAAAAkhSEU72QA0mRg8Uq8xmwuAACndCj8wAAAA%3D%3D&exvsurl=1&path=/calendar/item",
            "onlineMeetingUrl": null,
            "isOnlineMeeting": false,
            "onlineMeetingProvider": "unknown",
            "allowNewTimeProposals": true,
            "isDraft": false,
            "hideAttendees": false,
            "recurrence": null,
            "onlineMeeting": null,
            "responseStatus": {
                "response": "organizer",
                "time": "0001-01-01T00:00:00Z"
            },
            "body": {
                "contentType": "html",
                "content": ""
            },
            "start": {
                "dateTime": "2021-05-07T21:00:00.0000000",
                "timeZone": "UTC"
            },
            "end": {
                "dateTime": "2021-05-07T21:30:00.0000000",
                "timeZone": "UTC"
            },
            "location": {
                "displayName": "",
                "locationType": "default",
                "uniqueIdType": "unknown",
                "address": {},
                "coordinates": {}
            },
            "locations": [],
            "attendees": [],
            "organizer": {
                "emailAddress": {
                    "name": "Pierre Larochelle",
                    "address": "outlook_6DF706636259D6B2@outlook.com"
                }
            }
        }
    ]
}
```

#### Calendar create
POST https://graph.microsoft.com/v1.0/me/events
Body:
```
{
    "subject": "My new event",
    "start": {
        "dateTime": "2021-05-09T01:33:05.643Z",
        "timeZone": "UTC"
    },
    "end": {
        "dateTime": "2021-05-09T01:33:05.643Z",
        "timeZone": "UTC"
    }
}
```

## Links
* [Explorer](https://developer.microsoft.com/en-us/graph/graph-explorer)
* [Authentication](https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow)
* [Todo list API](https://docs.microsoft.com/en-us/graph/api/todotasklist-list-tasks?view=graph-rest-1.0&tabs=http)
* 
