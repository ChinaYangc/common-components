package com.fansz.apns.config;

public class FeedbackConnectionConfiguration {
    private int readTimeout = 1;

    public FeedbackConnectionConfiguration() {
    }

    public FeedbackConnectionConfiguration(final FeedbackConnectionConfiguration configuration) {
        this.readTimeout = configuration.readTimeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public void setReadTimeout(final int readTimeout) {
        if (readTimeout < 1) {
            throw new IllegalArgumentException("Read timeout must be greater than zero.");
        }

        this.readTimeout = readTimeout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + readTimeout;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FeedbackConnectionConfiguration other = (FeedbackConnectionConfiguration)obj;
        if (readTimeout != other.readTimeout)
            return false;
        return true;
    }
}
