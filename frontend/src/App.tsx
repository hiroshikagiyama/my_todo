import React, { useEffect, useState } from "react";
import "./App.css";

function App() {
    interface Todo {
        id: string;
        content: string;
        isCompleted: boolean;
    }

    const [todo, setTodo] = useState<Todo[]>([]);
    const [inputTodoContent, setInputTodoContent] = useState("");
    const [editTodoItem, setEditTodoItem] = useState<Todo | "">("");

    // todoItemの取得
    useEffect(() => {
        fetchGetTodo()
    }, []);

    // getTodo
    async function fetchGetTodo (){
        const response = await fetch(`/api/todo`);
        const data = await response.json();
        // stringの昇順
        data.sort((a: Todo, b: Todo) => a.content.localeCompare(b.content))
        setTodo(data)
    }

    // postTodo
    async function fetchPostTodo (){
        await fetch('/api/todo',{
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                content: inputTodoContent,
            }),
        })
    }

    // deleteTodo
    async function fetchDeleteTodo (deleteTodoItem: Todo){
        await fetch(`/api/todo/${deleteTodoItem.id}`,{
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(deleteTodoItem),
        })
    }

    // patchTodo
    async function fetchPatchTodo (updateTodoItem: Todo) {
        await fetch(`/api/todo/${updateTodoItem.id}`,{
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updateTodoItem),
        })
    }

    // todoItemの追加
    async function handleAddTodo() {
        await fetchPostTodo()
        await fetchGetTodo()
        setInputTodoContent("");
    }

    // todoItemの削除
    async function handleDeleteTodoItem(deleteTodoItem: Todo) {
        await fetchDeleteTodo(deleteTodoItem)
        await fetchGetTodo()
    }

    // input要素でentry keyが押された場合に登録、変更処理実行
    function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
        if (e.key === "Enter") {
            if (editTodoItem === "") {
                handleAddTodo();
            } else {
                handleAddEditedTodo();
            }
        }
    }

    // todoItemの完了
    async function handleTodoComplete(completedTodoItem: Todo) {
        const newCompletedTodoItem = {
            id: completedTodoItem.id,
            content: completedTodoItem.content,
            isCompleted: true,
        };
        await fetchPatchTodo(newCompletedTodoItem)
        await fetchGetTodo()
    }

    // todoItemの変更 DBには、データがある。front側のstateからのみ削除
    function handleEditTodoItem(editItem: Todo) {
        setEditTodoItem(editItem);
        setInputTodoContent(editItem.content);
        const newTodo = todo.filter(item => item.id !== editItem.id)
        setTodo(newTodo);
    }

    // 編集したtodoItemの登録
    async function handleAddEditedTodo() {
        if (editTodoItem === "") return;
        const editedTodoItem = {
            id: editTodoItem.id,
            content: inputTodoContent,
            isCompleted: false,
        };
        await fetchPatchTodo(editedTodoItem)
        await fetchGetTodo()
        setInputTodoContent("");
        setEditTodoItem("");
    }

    // キャンセルしたtodoItemを戻す
    function handleReverseEditedTodo() {
        if (editTodoItem === "") return;
        const newTodo = [...todo, editTodoItem];
        newTodo.sort((a: Todo, b: Todo) => a.content.localeCompare(b.content))
        setTodo(newTodo);
        setInputTodoContent("");
        setEditTodoItem("");
    }

    return (
        <>
            {/*タイトル*/}
            <div>My Todo</div>
            <hr />
            {/*Todo インプット*/}
            <div style={{ display: "flex", justifyContent: "center" }}>
                <input
                    type={"text"}
                    style={{ marginRight: "20px" }}
                    onKeyDown={handleKeyDown}
                    onChange={(e) => setInputTodoContent(e.target.value)}
                    value={inputTodoContent}
                />
                {editTodoItem === "" ? (
                    <button
                        disabled={inputTodoContent.length < 1}
                        onClick={handleAddTodo}
                    >
                        追加
                    </button>
                ) : (
                    <div style={{ display: "flex", gap: "20px" }}>
                        <button onClick={handleAddEditedTodo}>変更</button>
                        <button onClick={handleReverseEditedTodo}>キャンセル</button>
                    </div>
                )}
            </div>
            {/*Todoリスト*/}
            <div>
                <ul>
                    {todo.map((todoItem) => {
                        return (
                            todoItem.isCompleted || (
                                <div
                                    key={todoItem.id}
                                    style={{
                                        display: "flex",
                                        alignItems: "center",
                                        gap: "20px",
                                        marginBottom: "5px",
                                    }}
                                >
                                    <li
                                        style={{
                                            textAlign: "start",
                                            width: "200px",
                                        }}
                                    >
                                        {todoItem.content}
                                    </li>

                                    <div style={{ display: "flex", gap: "10px" }}>
                                        <button onClick={() => handleTodoComplete(todoItem)}>
                                            完了
                                        </button>
                                        <button onClick={() => handleEditTodoItem(todoItem)}>
                                            編集
                                        </button>
                                        <button onClick={() => handleDeleteTodoItem(todoItem)}>
                                            削除
                                        </button>
                                    </div>
                                </div>
                            )
                        );
                    })}
                </ul>
            </div>
        </>
    );
}

export default App;
