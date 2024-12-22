package com.email.spamfilter.bloomfilter.enums;

public enum FilterType {
        SPF,
        RECIEVED,
        KEYWORDS,
        ALL;

        public static FilterType fromString(String type) {
            try {
                return FilterType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid FilterType: " + type);
            }
        }
}
