package form

import lib.model.TodoCategory

case class CategoryFormData(
    name:  String, 
    slug:  String,
    color: Int//TodoCategory.ColorStatus
)