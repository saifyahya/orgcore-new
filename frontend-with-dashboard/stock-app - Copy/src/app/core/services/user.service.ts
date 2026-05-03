import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { User, AddUserRequest, Page } from '../models';

export interface UserPageFilter {
  page?: number;
  size?: number;
  search?: string;
  isActive?: number;
  branchId?: number;
  sortBy?: string;
  sortDir?: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private path = '/users';

  constructor(private api: ApiService) { }

  getAll(filter: UserPageFilter = {}): Observable<Page<User>> {
    const params: any = {
      page: filter.page ?? 0,
      size: filter.size ?? 10,
      sortBy: filter.sortBy ?? 'id',
      sortDir: filter.sortDir ?? 'desc'
    };

    if (filter.search) params.search = filter.search;
    if (filter.isActive !== undefined && filter.isActive !== null) params.isActive = filter.isActive;
    if (filter.branchId !== undefined && filter.branchId !== null) params.branchId = filter.branchId;

    return this.api.get<Page<User>>(this.path + '/getAllUsers', params);
  }

  getById(id: number): Observable<User> {
    return this.api.get<User>(`${this.path}/${id}`);
  }

  create(user: AddUserRequest): Observable<User> {
    return this.api.post<User>(`${this.path}/addUser`, user);
  }

  update(id: number, user: User): Observable<User> {
    return this.api.put<User>(`${this.path}`, user);
  }

  delete(id: number): Observable<void> {
    const user: User = { id, firstName: '', lastName: '', email: '', isActive: 0 };
    return this.api.put<void>(`${this.path}`, user);
  }
}
