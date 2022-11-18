/**
  * New file.
  * 
  */

package lib.model

import ixias.model._
// import ixias.util.EnumStatus

import java.time.LocalDateTime

// ユーザーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
import Todo._
case class Todo(
  id:        Option[Id],
  name:      String,
  slug:      String,
  color:     Int,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object Todo {

  val  Id = the[Identity[Id]]
  type Id = Long @@ Todo
  type WithNoId = Entity.WithNoId [Id, Todo]
  type EmbeddedId = Entity.EmbeddedId[Id, Todo]

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
      new Todo(
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

// case class Todo(
//   id: Option[Id],
//   category_id: Int,
//   title: String,
//   body: String,
//   state: Int,
//   updated_at: LocalDateTime = NOW,
//   created_at: LocalDateTime = NOW
// )