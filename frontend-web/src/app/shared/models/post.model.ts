import {Comment} from "./comment.model";

export class Post {
  id?: number;
  title: string;
  content: string;
  author: string;
  createdAt: string;
  status: string;
  reviewComment: string;
  commentResponseList?: Comment[] = [];

  constructor(title: string, content: string, author: string, createdAt: string, status: string, reviewComment: string) {
    this.title = title;
    this.content = content;
    this.author = author;
    this.createdAt = createdAt;
    this.status = status;
    this.reviewComment = reviewComment;
  }
}
