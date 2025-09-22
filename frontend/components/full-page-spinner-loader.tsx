import { Loader2 } from "lucide-react";

type FullPageSpinnerLoaderProps = {
  loadingMessage?: string;
};

export default function FullPageSpinnerLoader({
  loadingMessage,
}: FullPageSpinnerLoaderProps) {
  return (
    <div className="h-[100vh] w-full ">
      <div className="w-full h-full flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
        {loadingMessage && (
          <span className="text-gray-600 font-medium pl-3">
            {loadingMessage}
          </span>
        )}
      </div>
    </div>
  );
}
