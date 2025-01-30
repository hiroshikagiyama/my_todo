import '@testing-library/jest-dom/vitest';
import {describe, expect, test, vitest, afterEach} from 'vitest'
import {cleanup, render, screen, waitFor} from "@testing-library/react";
import App from "./App.tsx";
import {userEvent} from "@testing-library/user-event";

const spyFetchGetJson = vitest.hoisted(() => vitest.fn())
const mockFetch = vitest.fn().mockResolvedValue({json: spyFetchGetJson})
vitest.stubGlobal("fetch", mockFetch)

describe("App Tests", () => {

    afterEach(() => {
        // コンポーネントのクリーンアップ
        cleanup()
    });

    // データが存在しない場合、タイトルとボタンのみが表示される
    test('given data is not existed when renders app then see only title and text input and button', () => {
        spyFetchGetJson.mockResolvedValue([])

        render(<App/>)

        expect(screen.getByText("My Todo")).toBeInTheDocument()
        expect(screen.getByRole("textbox")).toBeInTheDocument()
        expect(screen.getByRole("button", {name: "追加"})).toBeInTheDocument()
    })
    // データが存在する場合、todo_item が表示される
    test("given data is existed when render then see todo items", async () => {
        spyFetchGetJson.mockResolvedValue([{id: "1", content: "todo1", isCompleted: false}])

        render(<App/>)

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo")
            expect(screen.getByText("todo1")).toBeInTheDocument()
        })
    })

    // todo_itemが入力されている場合、追加ボタンをクリックするとtodo_itemが追加される
    test("when write new item and click button then fetch post request", async () => {
        spyFetchGetJson.mockResolvedValue([{id: "1", content: "Hello World", isCompleted: false}])

        render(<App/>)

        await userEvent.type(screen.getByRole("textbox"), "Hello World")
        await userEvent.click(screen.getByRole("button", {name: "追加"}))

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({content: "Hello World"}),
            })
            expect(screen.getByText("Hello World")).toBeInTheDocument()
            expect(screen.getByRole("textbox")).toHaveValue("")
        })
    })
    // 完了のtodo_itemは一覧に表示されない
    test("when data is existed but completed status when render then cannot see todo item", async () => {
        spyFetchGetJson.mockResolvedValue([
            {id: "1", content: "Hello World", isCompleted: true},
            {id: "2", content: "Jun desu", isCompleted: false}
        ])

        render(<App/>)
        expect(await screen.findByText("Jun desu")).toBeInTheDocument()
        // getByTextだと、表示されない要素はエラーとなるため、要素がないかテストするときは、queryByTextを使う
        expect(screen.queryByText("Hello World")).not.toBeInTheDocument()
    })

    // todo_itemが入力されている場合、Enterキーを押すとtodo_itemが追加される
    test("when write new item press enter then fetch post request", async () => {
        spyFetchGetJson.mockResolvedValue([{id: "1", content: "Hello World", isCompleted: false}])

        render(<App/>)

        await userEvent.type(screen.getByRole("textbox"), "Hello World")
        await userEvent.keyboard('{Enter}')

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({content: "Hello World"}),
            })
            expect(screen.getByText("Hello World")).toBeInTheDocument()
            expect(screen.getByRole("textbox")).toHaveValue("")
        })
    })

    // todo_item入力内容が空の場合、追加ボタンがdisabled:trueである
    test("The add button should be disabled when the todo item input is empty", async ()=>{
        // spyFetchGetJson.mockResolvedValue([])

        render(<App/>)

        const todoInput = screen.getByRole('textbox');
        await userEvent.clear(todoInput)

        expect(todoInput).toHaveValue('')
        expect(screen.getByRole("button", {name: "追加"})).toBeDisabled();
    })

})

// query function -> getBy-, findBy-, queryBy-...
// getBy- 1. data O -> return DOM object, data X -> throw exception
// queryBy- 1. data O -> return DOM object, data X -> return null
// findBy - 1. data O -> return Promise(DOM), data X -> throw exception