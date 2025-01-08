export class Comment {
  id?: number;
  postId: number;
  commenter: string;
  text: string;
  addedAt: string;

  constructor(postId: number, commenter: string, text: string, addedAt: string) {
    this.postId = postId;
    this.commenter = commenter;
    this.text = text;
    this.addedAt = addedAt;
  }
}

