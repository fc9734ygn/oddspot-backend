package com.homato.data.util

enum class PostgreSQLErrorCode(val code: String) {
    UNIQUE_VIOLATION("23505"),
    FOREIGN_KEY_VIOLATION("23503"),
    SYNTAX_ERROR("42601"),
    INVALID_PARAMETER_VALUE("22P02"),
    NULL_VALUE_NOT_ALLOWED("23502"),
    CHECK_VIOLATION("23514"),
    DATA_TYPE_MISMATCH("42804"),
    INSUFFICIENT_PRIVILEGE("42501"),
    DUPLICATE_DATABASE("42P04"),
    DUPLICATE_TABLE("42P07"),
    DUPLICATE_SCHEMA("42P06");

    companion object {
        fun fromCode(code: String): PostgreSQLErrorCode? {
            return values().firstOrNull { it.code == code }
        }
    }
}
