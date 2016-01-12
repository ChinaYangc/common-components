package com.fansz.apns.config;

public class PushManagerConfiguration {

    private int concurrentConnectionCount = 1;

    private ApnsConnectionConfiguration connectionConfiguration = new ApnsConnectionConfiguration();

    private FeedbackConnectionConfiguration feedbackConfiguration = new FeedbackConnectionConfiguration();

    public PushManagerConfiguration() {
    }

    public PushManagerConfiguration(final PushManagerConfiguration configuration) {
        this.concurrentConnectionCount = configuration.getConcurrentConnectionCount();

        this.connectionConfiguration = new ApnsConnectionConfiguration(configuration.getConnectionConfiguration());
        this.feedbackConfiguration = new FeedbackConnectionConfiguration(
                configuration.getFeedbackConnectionConfiguration());
    }

    public int getConcurrentConnectionCount() {
        return concurrentConnectionCount;
    }

    public void setConcurrentConnectionCount(int concurrentConnectionCount) {
        this.concurrentConnectionCount = concurrentConnectionCount;
    }

    public ApnsConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    public void setConnectionConfiguration(final ApnsConnectionConfiguration connectionConfiguration) {
        if (connectionConfiguration == null) {
            throw new NullPointerException("Connection configuration must not be null.");
        }

        this.connectionConfiguration = connectionConfiguration;
    }

    public FeedbackConnectionConfiguration getFeedbackConnectionConfiguration() {
        return this.feedbackConfiguration;
    }

    public void setFeedbackConnectionConfiguration(final FeedbackConnectionConfiguration feedbackConnectionConfiguration) {
        if (feedbackConnectionConfiguration == null) {
            throw new NullPointerException("Feedback connection configuration must not be null.");
        }

        this.feedbackConfiguration = feedbackConnectionConfiguration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + concurrentConnectionCount;
        result = prime * result + ((connectionConfiguration == null) ? 0 : connectionConfiguration.hashCode());
        result = prime * result + ((feedbackConfiguration == null) ? 0 : feedbackConfiguration.hashCode());
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
        final PushManagerConfiguration other = (PushManagerConfiguration)obj;
        if (concurrentConnectionCount != other.concurrentConnectionCount)
            return false;
        if (connectionConfiguration == null) {
            if (other.connectionConfiguration != null)
                return false;
        }
        else if (!connectionConfiguration.equals(other.connectionConfiguration))
            return false;
        if (feedbackConfiguration == null) {
            if (other.feedbackConfiguration != null)
                return false;
        }
        else if (!feedbackConfiguration.equals(other.feedbackConfiguration))
            return false;
        return true;
    }
}
