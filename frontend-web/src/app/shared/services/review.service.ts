import {inject, Injectable} from '@angular/core';
import {environment} from "../../../environments/environment";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private baseUrl = environment.reviewApiUrl + "/api/review";
  http: HttpClient = inject(HttpClient);
  authService: AuthService = inject(AuthService);

  reviewPost(postId: number, editor: string, approved: boolean, reviewComment: string): Observable<any> {
    const reviewData = { editor, approved, reviewComment };
    const headers = this.createHeaders();
    return this.http.post(`${this.baseUrl}/${postId}`, reviewData, { headers });
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
}
