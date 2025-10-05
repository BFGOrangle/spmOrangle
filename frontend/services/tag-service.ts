import { AuthenticatedApiClient } from "./authenticated-api-client";

export interface TagResponse {
  id: number;
  tagName: string;
}

export interface CreateTagRequest {
  tagName: string;
}

export class TagService {
  private authenticatedClient: AuthenticatedApiClient;

  constructor() {
    this.authenticatedClient = new AuthenticatedApiClient();
  }

  async getTags(): Promise<TagResponse[]> {
    const tags = await this.authenticatedClient.get<TagResponse[]>("/api/tag");
    if (!Array.isArray(tags)) {
      return [];
    }

    return [...tags].sort((a, b) => a.tagName.localeCompare(b.tagName));
  }

  async createTag(request: CreateTagRequest | string): Promise<TagResponse> {
    const payload = typeof request === "string" ? { tagName: request } : request;
    return this.authenticatedClient.post<TagResponse>("/api/tag", payload);
  }
}

export const tagService = new TagService();
