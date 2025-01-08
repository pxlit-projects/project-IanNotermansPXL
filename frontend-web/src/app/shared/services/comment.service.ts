import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from "@angular/common/http";
import {AuthService} from "./auth.service";
import {environment} from "../../../environments/environment";
import {catchError, Observable, throwError} from "rxjs";
import {Comment} from "../models/comment.model";

interface AddCommentRequest {
  postId: number;
  text: string;
}

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private baseUrl = environment.commentApiUrl + "/api/comments";
  http: HttpClient = inject(HttpClient);
  authService: AuthService = inject(AuthService);

  // Get all comments for a post
  getCommentsByPostId(postId: number): Observable<Comment[]> {
    const headers = this.createHeaders();
    return this.http
      .get<Comment[]>(`${this.baseUrl}/${postId}`, { headers })
      .pipe(catchError(this.handleError));
  }

  // Add a new comment
  addComment(request: AddCommentRequest): Observable<Comment> {
    const headers = this.createHeaders();
    return this.http
      .post<Comment>(this.baseUrl, request, { headers })
      .pipe(catchError(this.handleError));
  }

  updateComment(commentId: number, text: string): Observable<Comment> {
    const headers = this.createHeaders();
    const body = { text }; // Wrap text in an object
    return this.http
      .put<Comment>(`${this.baseUrl}/${commentId}`, body, { headers })
      .pipe(catchError(this.handleError));
  }

  // Delete a comment
  deleteComment(commentId: number): Observable<void> {
    const headers = this.createHeaders();
    return this.http
      .delete<void>(`${this.baseUrl}/${commentId}`, { headers })
      .pipe(catchError(this.handleError));
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
