package persistence.config

import eu.timepit.refined.types.string.NonEmptyString

final case class DBConfig(
    driver: NonEmptyString,
    url: NonEmptyString,
    user: String,
    password: String
)
