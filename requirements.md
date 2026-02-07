## High Level Goal
This is a meal planning application to be used by a family to plan their meals and shopping for the week ahead.  It is primarily a time saving application, both with meal planning and shopping.

### Users
The will be one technically competent administrator who can install and manage the software, but the rest should be usable by less technical members of the family.

At this stage it does not need authentication.

## Data model
We will need to store information about meals. This should include a title, description, ingredients, and time/effort to cook.
The should be users/family members. We will need to store there meal preferences, both for eating, and preparation/cooking.
Finally we need to be able to store the meal plan for the week ahead.  We should also track historical data.

## API
A rest API should be provided for accessing and updating the various data models

## UI
This will need a web interface.  We will need CRUD operations for inputting meal, and person data. 
We will need user friendly interface for choosing meals for the week, and being able to re-arrange them, ideally with drag and drop.
There should be as way to rate meals after they've been eaten.
There should be a way to export a shopping list using the ingredients required for the meals for the week ahead.

## Technology choices
This should be a Java Spring Boot application for the backend, and a react front end.  We will need to be able to host it on a raspberry pi.
