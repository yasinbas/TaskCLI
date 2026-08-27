# Task Tracker CLI

A simple Command Line Interface (CLI) application built with Java to track and manage your tasks. This project uses native Java file system modules and basic JSON parsing without any external dependencies.

Project link: https://roadmap.sh/projects/task-tracker

## Features
- Add, update, and delete tasks
- Mark tasks as `in-progress` or `done`
- List all tasks or filter them by status (`todo`, `in-progress`, `done`)
- Automatic persistent JSON storage (`tasks.json`)

## Requirements
- Java Development Kit (JDK) 11 or higher

## How to Run

1. **Compile the project:**
   ```bash
   javac -encoding UTF-8 TaskCli.java


# Add a new task
java TaskCli add "Buy groceries"

# Update a task
java TaskCli update 1 "Buy groceries and cook dinner"

# Delete a task
java TaskCli delete 1

# Mark status
java TaskCli mark-in-progress 1
java TaskCli mark-done 1

# List tasks
java TaskCli list
java TaskCli list done
java TaskCli list todo
java TaskCli list in-progress