/**
  * New file.
  * 
  */

package lib.model

import ixias.model._
// import ixias.util.EnumStatus

import java.time.LocalDateTime

import TodoCategory._
case class TodoCategory(
  id:        Option[Id],
  name:      String,
  slug:      String,
  color:     Int,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object TodoCategory {

  val  Id = the[Identity[Id]]
  type Id = Long @@ TodoCategory
  type WithNoId = Entity.WithNoId [Id, TodoCategory]
  type EmbeddedId = Entity.EmbeddedId[Id, TodoCategory]

  // INSERT時のIDがAutoincrementのため,IDなしであることを示すオブジェクトに変換
  def apply(name: String, slug: String, color: Int): WithNoId = {
    new Entity.WithNoId(
      new TodoCategory(
        id    = None,
        name  = name,
        slug  = slug,
        color = color,
      )
    )
  }
}