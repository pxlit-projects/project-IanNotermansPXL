import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from "@angular/common/http";
import {catchError, Observable, throwError} from "rxjs";
import {environment} from "../../../environments/environment";
import {Post} from "../models/post.model";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private baseUrl = environment.postApiUrl + "/api/posts";
  http: HttpClient = inject(HttpClient);
  authService: AuthService = inject(AuthService);

  createPost(post: Post): Observable<Post> {
    const headers = this.createHeaders();
    return this.http.post<Post>(this.baseUrl, post, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  getPostById(id: number): Observable<Post> {
    const headers = this.createHeaders();
    return this.http.get<Post>(`${this.baseUrl}/${id}`, { headers });
  }

  getPostsByStatus(status: string): Observable<Post[]> {
    const headers = this.createHeaders();
    return this.http.get<Post[]>(`${this.baseUrl}/status/${status}`, { headers });
  }

  getAllPublishedPosts(): Observable<Post[]> {
    const headers = this.createHeaders();
    return this.http.get<Post[]>(`${this.baseUrl}/publishedPosts`, { headers });
  }

  getAllNotPublishedPosts(): Observable<Post[]> {
    const headers = this.createHeaders();
    return this.http.get<Post[]>(`${this.baseUrl}/not-published`, { headers });
  }

  updatePost(id: number, post: Post): Observable<Post> {
    const headers = this.createHeaders();
    return this.http.put<Post>(`${this.baseUrl}/${id}`, post, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  publishPost(id: number): Observable<Post> {
    const headers = this.createHeaders();
    return this.http.put<Post>(`${this.baseUrl}/${id}/publish`, {}, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  private createHeaders(): HttpHeaders {
    const currentUser = this.authService.currentUser;
    if (currentUser) {
      return new HttpHeaders({
        user: currentUser.username,
        role: currentUser.role
      });
    }
    return new HttpHeaders();
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    console.error('An error occurred:', error.message);
    return throwError(() => new Error('Something bad happened; please try again later.'));
  }

}


