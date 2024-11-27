import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {catchError, Observable, throwError} from "rxjs";
import {environment} from "../../../environments/environment";
import {Post} from "../models/post.model";

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private baseUrl = environment.postApiUrl + "/api/posts";
  http: HttpClient = inject(HttpClient);

  createPost(post: Post): Observable<Post> {
    return this.http.post<Post>(this.baseUrl, post).pipe(
      catchError(this.handleError)
    );
  }
  getAllPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(this.baseUrl);
  }

  getPostById(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.baseUrl}/${id}`);
  }

  getPostsByStatus(status: string): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.baseUrl}/status/${status}`);
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    console.error('An error occurred:', error.message);
    return throwError(() => new Error('Something bad happened; please try again later.'));
  }
}

