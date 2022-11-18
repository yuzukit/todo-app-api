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

  // // ステータス定義
  // //~~~~~~~~~~~~~~~~~
  // sealed abstract class Status(val code: Short, val name: String) extends EnumStatus
  // object Status extends EnumStatus.Of[Status] {
  //   case object IS_INACTIVE extends Status(code = 0,   name = "無効")
  //   case object IS_ACTIVE   extends Status(code = 100, name = "有効")
  // }

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

  // def build(name: String): Todo#WithNoId =
  //   new Todo(
  //     id = None,
  //     name = name,
  //     slug  = slug,
  //     color = color,
  //   ).toWithNoId
}