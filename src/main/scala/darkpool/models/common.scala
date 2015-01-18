package darkpool.models

import java.util.UUID

import com.github.nscala_time.time.Imports._

/**
 * Created by: film42 on: 1/17/15.
 */
package object common {
  trait CreatedAt {
    def createdAt: DateTime = DateTime.now
  }

  trait ID {
    def id: UUID
  }

  trait Quantity {
    def quantity: Double
  }
}
