import zio.{Has, RIO, Task, ZIO}

package object persistence {
  type CustomerService = Has[CustomerService.Service]


}
