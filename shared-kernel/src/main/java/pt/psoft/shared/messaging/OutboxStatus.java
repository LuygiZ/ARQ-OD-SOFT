package pt.psoft.shared.messaging;

public enum OutboxStatus {
    PENDING,    // Aguardando publicação
    PUBLISHED,  // Publicado com sucesso
    FAILED      // Falha na publicação (após retries)
}
