/**
  * New file.
  * 
  */

package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

// ユーザーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
import Todo._
case class Todo(
  id:          Option[Id],
  category_id: TodoCategory.Id,
  title:       String,
  body:        String,
  state:       Status,
  updatedAt:   LocalDateTime = NOW,
  createdAt:   LocalDateTime = NOW
) extends EntityModel[Todo.Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object Todo {

  val  Id = the[Identity[Id]]
  type Id = Long @@ Todo
  type WithNoId   = Entity.WithNoId  [Id, Todo]
  type EmbeddedId = Entity.EmbeddedId[Id, Todo]

  // ステータス定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Status(val code: Short, val name: String) extends EnumStatus
  object Status extends EnumStatus.Of[Status] {
    //type Status = values
    case object IS_UNTOUCHED extends Status(code = 0, name = "未着手") //未着手
    case object IS_ONGOING   extends Status(code = 1, name = "進行中") //進行中
    case object IS_FINISHED  extends Status(code = 2, name = "完了") //完了

    //lazy val values: IndexedSeq[Status] = findValues
    val statusSeq = Status.values.map(state => (state.code.toString, state.name))
  }

  // INSERT時のIDがAutoincrementのため,IDなしであることを示すオブジェクトに変換
  def apply(category_id: TodoCategory.Id, title: String, body: String, state: Status): WithNoId = {
    new Entity.WithNoId(
      new Todo(
        id          = None,
        category_id = category_id,
        title       = title,
        body        = body,
        state       = state,
      )
    )
  }

}