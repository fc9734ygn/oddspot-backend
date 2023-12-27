package com.homato.service.authentication

@Suppress("UnusedPrivateMember")
object SignUpValidator {

    private const val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
    private const val PASSWORD_LENGTH_MAX = 100
    private const val PASSWORD_LENGTH_MIN = 8

    fun validateEmail(email: String): String? {
        return if (EMAIL_REGEX.toRegex().matches(email)) return null else "Invalid email"
    }

    fun validatePassword(password: String) : String? = when {
            isTooShort(password) -> "Password too short"
            isTooLong(password) -> "Password too long"
            isCommon(password) -> "Password too common"
            !containsUppercase(password) -> "Password must contain at least one uppercase letter"
            !containsLowercase(password) -> "Password must contain at least one lowercase letter"
            !containsNumber(password) -> "Password must contain at least one number"
            else -> null
        }

    private fun isLengthValid(password: String): Boolean =
        password.length in PASSWORD_LENGTH_MIN..PASSWORD_LENGTH_MAX

    private fun isTooLong(password: String) = password.length > PASSWORD_LENGTH_MAX

    private fun isTooShort(password: String) = password.length < PASSWORD_LENGTH_MIN

    private fun isCommon(password: String) =
        commonPasswords.contains(password)

    private fun containsUppercase(password: String) = password.contains("[A-Z]".toRegex())

    private fun containsLowercase(password: String) = password.contains("[a-z]".toRegex())

    private fun containsNumber(password: String) = password.contains("[0-9]".toRegex())

    private fun containsSymbol(password: String) =
        password.contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())

    private val commonPasswords = listOf(
        "123456",
        "password",
        "12345678",
        "qwerty",
        "123456789",
        "12345",
        "1234",
        "111111",
        "1234567",
        "dragon",
        "123123",
        "baseball",
        "abc123",
        "football",
        "monkey",
        "letmein",
        "696969",
        "shadow",
        "master",
        "666666",
        "qwertyuiop",
        "123321",
        "mustang",
        "1234567890",
        "michael",
        "654321",
        "pussy",
        "superman",
        "1qaz2wsx",
        "7777777",
        "fuckyou",
        "121212",
        "000000",
        "qazwsx",
        "123qwe",
        "killer",
        "trustno1",
        "jordan",
        "jennifer",
        "zxcvbnm",
        "asdfgh",
        "hunter",
        "buster",
        "soccer",
        "harley",
        "batman",
        "andrew",
        "tigger",
        "sunshine",
        "iloveyou",
        "fuckme",
        "2000",
        "charlie",
        "robert",
        "thomas",
        "hockey",
        "ranger",
        "daniel",
        "starwars",
        "klaster",
        "112233",
        "george",
        "asshole",
        "computer",
        "michelle",
        "jessica",
        "pepper",
        "1111",
        "zxcvbn",
        "555555",
        "11111111",
        "131313",
        "freedom",
        "777777",
        "pass",
        "fuck",
        "maggie",
        "159753",
        "aaaaaa",
        "ginger",
        "princess",
        "joshua",
        "cheese",
        "amanda",
        "summer",
        "love",
        "ashley",
        "6969",
        "nicole",
        "chelsea",
        "biteme",
        "matthew",
        "access",
        "yankees",
        "987654321",
        "dallas",
        "austin",
        "thunder",
        "taylor",
        "matrix",
        "minecraft"
    )
}