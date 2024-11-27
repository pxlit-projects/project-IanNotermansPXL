export class Post {
  id?: number;
  title: string;
  content: string;
  author: string;
  createdAt: string;
  status: string;


  constructor(title: string, content: string, author: string, createdAt: string, status: string) {
    this.title = title;
    this.content = content;
    this.author = author;
    this.createdAt = createdAt;
    this.status = status;
  }
}
