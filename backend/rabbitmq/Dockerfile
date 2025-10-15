FROM rabbitmq:3.13-management

# Set environment variables for default user and password
ENV RABBITMQ_DEFAULT_USER=admin \
    RABBITMQ_DEFAULT_PASS=admin

# Expose AMQP and Management UI ports
EXPOSE 5672 15672

# Enable management plugin (already included in the management tag, but explicitly enabling)
RUN rabbitmq-plugins enable --offline rabbitmq_management

# Optional: Add custom configuration if needed
# COPY rabbitmq.conf /etc/rabbitmq/rabbitmq.conf

# Health check
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD rabbitmq-diagnostics -q ping

# Use the default entrypoint from the base image
