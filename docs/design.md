# Problem statement
It's too easy to lose, forget about, or just ignore your todo list. This is made worse when the task is ill-defined.

People tend to do things when they've made explicit time for the task. Even if the task is ill-defined, this explicit time will provide the space to define the task and ultimately complete it.

Our solution is to take each todo item and schedule the time needed to complete it. The user will be expected to provide a due date as well as an estimate for the time it would take to complete hte task.
# Basic design

First a few terms:
* Time estimate - Number of hours (4h) or days (5d = 5d*6h/d = 30 ideal hours)
* Task - A todo item with a time estimate.
* Existing event - Event in the user's calendar. This could be tied to an existing task (by task ID) or unassociated.
* Calendar hole - A space between existing events that a new task event can fit into.

The main point of this project is to creat the correct amount of time to do a task.
1. Grab all todo items
2. Get all past and future events (90 days each way?)
3. Match calendar events to items to understand what's not accounted for
4. For each task

   1. Get all associated events
   2. Sum the event times and subtract that from the task estimate
   3. If the task's remaining estimate is non-positive then log something about it (V1) and remove it from the list

   At this point we only have tasks for which we need to create events to fill the remaining estimate

5. Find all holes in the future scope. We should consider things like start and end time, weekends, etc.
6. For each task (ordered by priority), work backward from the due date and create events. 

## Open questions
> What to do about the current event, if any?
The current event should not be treated differently. We're counting against scheduled time both past and future.

