package effect.telemetry

import zio.Has

package object http {
  type Client = Has[Client.Service]
}
