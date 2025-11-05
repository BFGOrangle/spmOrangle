import { TagService } from "../../services/tag-service";
import { AuthenticatedApiClient } from "../../services/authenticated-api-client";

jest.mock("../../services/authenticated-api-client");

const MockAuthenticatedApiClient = AuthenticatedApiClient as jest.MockedClass<typeof AuthenticatedApiClient>;

describe("TagService", () => {
  let service: TagService;
  let mockAuthenticatedClient: jest.Mocked<AuthenticatedApiClient>;

  beforeEach(() => {
    mockAuthenticatedClient = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
      patch: jest.fn(),
      postMultipart: jest.fn(),
    } as any;

    MockAuthenticatedApiClient.mockImplementation(() => mockAuthenticatedClient);
    service = new TagService();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe("getTags", () => {
    it("returns sorted tags", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([
        { id: 2, tagName: "beta", deleteInd: false },
        { id: 1, tagName: "alpha", deleteInd: false },
        { id: 3, tagName: "gamma", deleteInd: false },
      ]);

      const result = await service.getTags();

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith("/api/tag");
      expect(result.map((tag) => tag.tagName)).toEqual(["alpha", "beta", "gamma"]);
    });

    it("handles non-array responses", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce({ message: "unexpected" } as any);

      const result = await service.getTags();

      expect(result).toEqual([]);
    });
  });

  describe("createTag", () => {
    it("creates tag from string input", async () => {
      const payload = { id: 99, tagName: "frontend", deleteInd: false };
      mockAuthenticatedClient.post.mockResolvedValueOnce(payload);

      const result = await service.createTag("frontend");

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith("/api/tag", { tagName: "frontend" });
      expect(result).toEqual(payload);
    });

    it("creates tag from object input", async () => {
      const payload = { id: 100, tagName: "backend", deleteInd: false };
      mockAuthenticatedClient.post.mockResolvedValueOnce(payload);

      const result = await service.createTag({ tagName: "backend" });

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith("/api/tag", { tagName: "backend" });
      expect(result).toEqual(payload);
    });
  });

  describe("deleteTag", () => {
    it("deletes tag by id", async () => {
      mockAuthenticatedClient.delete.mockResolvedValueOnce(undefined);

      await service.deleteTag(123);

      expect(mockAuthenticatedClient.delete).toHaveBeenCalledWith("/api/tag/123");
    });
  });
});
