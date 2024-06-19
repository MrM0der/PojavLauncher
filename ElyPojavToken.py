import requests

def authenticate_user(username, password, client_token):
    url = "https://authserver.ely.by/auth/authenticate"  # Замените на ваш URL

    # Параметры запроса
    payload = {
        "username": username,
        "password": password,
        "clientToken": client_token,
        "requestUser": True
    }

    try:
        response = requests.post(url, json=payload)
        response_data = response.json()

        if response.status_code == 200:
            # Успешная авторизация
            access_token = response_data["accessToken"]
            client_token = response_data["clientToken"]
            user_info = response_data.get("user", {})
            return access_token, client_token, user_info
        elif response.status_code == 401:
            # Ошибка: аккаунт защищён двухфакторной аутентификацией
            error_message = response_data.get("errorMessage", "Unknown error")
            print(f"Ошибка: {error_message}")
        else:
            print(f"Неожиданный статус код: {response.status_code}")
    except requests.RequestException as e:
        print(f"Ошибка при выполнении запроса: {e}")

# Пример использования
if __name__ == "__main__":
    username = input("Введите своё ely.by имя: ")
    password = input("Введи свой ely.by пароль: ")
    client_token = "client_token"

    access_token, client_token, user_info = authenticate_user(username, password, client_token)
    print(f"Access Token: {access_token}")
    #print(f"Client Token: {client_token}")
    print(f"User Info: {user_info}")
