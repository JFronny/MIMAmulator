package de.frohnmeyerwds.mima.util

sealed interface Either<L, R> {
    data class Left<L, R>(val value: L) : Either<L, R>
    data class Right<L, R>(val value: R) : Either<L, R>
}

fun <L, R> L.eitherLeft(): Either<L, R> = Either.Left(this)
fun <L, R> R.eitherRight(): Either<L, R> = Either.Right(this)
fun <L, R, Q> Either<L, R>.fold(left: (L) -> Q, right: (R) -> Q): Q = when (this) {
    is Either.Left -> left(value)
    is Either.Right -> right(value)
}