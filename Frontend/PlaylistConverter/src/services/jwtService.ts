export const jwtService = {
    getToken: () => localStorage.getItem("token"),
    setToken: (token: string) => localStorage.setItem("token", token),
    removeToken: () => localStorage.removeItem("token"),
    isLoggedIn: () => !!localStorage.getItem("token"),
};
