-- Add delete_ind column to tag table for soft-delete functionality
-- Only MANAGER role can soft-delete tags
-- Deleted tags will be displayed grayed out in the UI

ALTER TABLE syncup.tag
ADD COLUMN delete_ind BOOLEAN NOT NULL DEFAULT false;

-- Add index for faster queries filtering out deleted tags
CREATE INDEX idx_tag_delete_ind ON syncup.tag(delete_ind);

-- Add comment for documentation
COMMENT ON COLUMN syncup.tag.delete_ind IS 'Soft delete flag - when true, tag is considered deleted but remains visible';
