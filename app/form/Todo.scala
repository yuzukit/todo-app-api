package form

import lib.model.Todo

case class TodoFormData(
    category_id: Int, 
    title:       String, 
    body:        String, 
    state:       Int//Todo.Status
)