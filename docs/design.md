# Problem statement
It's too easy to lose, forget about, or just ignore your todo list. This is made worse when the task is ill-defined.

People tend to do things when they've made explicit time for the task. Even if the task is ill-defined, this explicit time will provide the space to define the task and ultimately complete it.

Our solution is to take each todo item and schedule the time needed to complete it. The user will be expected to provide a due date as well as an estimate for the time it would take to complete hte task.
# Basic design
## Creating events
The main point of this project is to creat the correct amount of time to do a task.
1. Grab all todo items
2. Grab all future calendar events (include the current event?)
3. Match calendar events to items to understand what's not accounted for
4. Create new events for what's not scheduled to match the estimate

