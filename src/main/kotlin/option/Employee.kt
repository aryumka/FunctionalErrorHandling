package aryumka.option

data class Employee(
  val name: String,
  val department: String,
  val manager: Option<String>
)

fun lookupByName(name: String): Option<Employee> = TODO()

fun timDepartment(): Option<String> =
  lookupByName("Tim")
    .map { it.department }

val manager: String = lookupByName("Tim")
  .flatMap { it.manager }
  .getOrElse { "Unemployed" }