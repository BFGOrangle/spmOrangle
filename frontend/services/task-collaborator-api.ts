import {
  AuthenticatedApiClient,
  BaseApiError,
  BaseValidationError,
} from "./authenticated-api-client";
import {
  AddCollaboratorRequestDto,
  AddCollaboratorResponseDto,
  CollaboratorApiErrorDetail,
  RemoveCollaboratorRequestDto,
} from "@/types/collaborator";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8088";
const TASK_COLLABORATOR_ENDPOINT = `${API_BASE_URL}/api/tasks/collaborator`;

export class CollaboratorApiError extends BaseApiError {
  constructor(
    status: number,
    statusText: string,
    errors: CollaboratorApiErrorDetail[] = [],
    timestamp?: string,
  ) {
    super(status, statusText, errors, timestamp);
    this.name = "CollaboratorApiError";
  }
}

export class CollaboratorValidationError extends BaseValidationError {
  constructor(
    public validationErrors: Array<{
      message: string;
      field: string;
      rejectedValue?: unknown;
      timestamp: string;
    }>,
    status: number = 400,
    statusText: string = "Validation Error",
  ) {
    super(validationErrors, status, statusText);
    this.name = "CollaboratorValidationError";
  }
}

class TaskCollaboratorApiClient extends AuthenticatedApiClient {
  async deleteWithBody<T>(url: string, data: any): Promise<T> {
    return this.request<T>(url, {
      method: "DELETE",
      body: JSON.stringify(data),
    });
  }

  protected async handleErrorResponse(response: Response): Promise<never> {
    let errorData: unknown;

    try {
      errorData = await response.json();
    } catch {
      throw new CollaboratorApiError(response.status, response.statusText, [
        {
          message: "An unexpected error occurred",
          timestamp: new Date().toISOString(),
        },
      ]);
    }

    const errorResponse = errorData as {
      error?: string;
      message?: string;
      errors?: CollaboratorApiErrorDetail[];
      timestamp?: string;
    };

    if (response.status === 400 && Array.isArray(errorResponse.errors)) {
      const validationErrors = errorResponse.errors.map((error) => ({
        message: error.message || "Validation error",
        field: error.field ?? "",
        rejectedValue: error.rejectedValue,
        timestamp:
          error.timestamp ||
          errorResponse.timestamp ||
          new Date().toISOString(),
      }));

      throw new CollaboratorValidationError(validationErrors);
    }

    const normalizedErrors = Array.isArray(errorResponse.errors)
      ? errorResponse.errors.map((error) => ({
          message:
            error.message ||
            errorResponse.error ||
            errorResponse.message ||
            "An error occurred",
          field: error.field,
          rejectedValue: error.rejectedValue,
          timestamp:
            error.timestamp ||
            errorResponse.timestamp ||
            new Date().toISOString(),
        }))
      : [
          {
            message:
              errorResponse.error ||
              errorResponse.message ||
              "An error occurred",
            timestamp: errorResponse.timestamp || new Date().toISOString(),
          },
        ];

    throw new CollaboratorApiError(
      response.status,
      response.statusText,
      normalizedErrors,
      errorResponse.timestamp,
    );
  }
}

export class TaskCollaboratorApiService {
  private client = new TaskCollaboratorApiClient();

  async addCollaborator(
    payload: AddCollaboratorRequestDto,
  ): Promise<AddCollaboratorResponseDto> {
    return this.client.post<AddCollaboratorResponseDto>(
      TASK_COLLABORATOR_ENDPOINT,
      payload,
    );
  }

  async removeCollaborator(payload: RemoveCollaboratorRequestDto): Promise<void> {
    await this.client.deleteWithBody<void>(TASK_COLLABORATOR_ENDPOINT, payload);
  }
}

export const taskCollaboratorApiService = new TaskCollaboratorApiService();
