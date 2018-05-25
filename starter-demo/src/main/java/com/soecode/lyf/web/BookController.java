package com.soecode.lyf.web;

import com.github.datalking.annotation.Autowired;
import com.github.datalking.annotation.Controller;
import com.github.datalking.annotation.web.PathVariable;
import com.github.datalking.annotation.web.RequestMapping;
import com.github.datalking.annotation.web.RequestParam;
import com.github.datalking.annotation.web.ResponseBody;
import com.github.datalking.web.http.RequestMethod;
import com.github.datalking.web.mvc.Model;
import com.soecode.lyf.dto.AppointExecution;
import com.soecode.lyf.dto.Result;
import com.soecode.lyf.entity.Book;
import com.soecode.lyf.enums.AppointStateEnum;
import com.soecode.lyf.exception.NoNumberException;
import com.soecode.lyf.exception.RepeatAppointException;
import com.soecode.lyf.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/book")
public class BookController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BookService bookService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    private String list(Model model) {
        List<Book> list = bookService.getList();
//        System.out.println("===== /list");
//        System.out.println(list.get(0));
        model.addAttribute("list", list);
        // list.jsp + model = ModelAndView
        // WEB-INF/jsp/"list".jsp
        return "list";
    }

    @RequestMapping(value = "/{bookId}/detail", method = RequestMethod.GET)
    private String detail(@PathVariable("bookId") Long bookId, Model model) {

        if (bookId == null) {
            return "redirect:/book/list";
        }

        Book book = bookService.getById(bookId);

        if (book == null) {
            return "forward:/book/list";
        }

        model.addAttribute("book", book);
        return "detail";
    }

    // ajax json
    @RequestMapping(value = "/{bookId}/appoint", method = RequestMethod.POST, produces = {"application/json; charset=utf-8"})
    @ResponseBody
    private Result<AppointExecution> appoint(@PathVariable("bookId") Long bookId, @RequestParam("studentId") Long studentId) {
        if (studentId == null || studentId.equals("")) {
            return new Result<>(false, "学号不能为空");
        }
        AppointExecution execution = null;
        try {
            execution = bookService.appoint(bookId, studentId);
        } catch (NoNumberException e1) {
            execution = new AppointExecution(bookId, AppointStateEnum.NO_NUMBER);
        } catch (RepeatAppointException e2) {
            execution = new AppointExecution(bookId, AppointStateEnum.REPEAT_APPOINT);
        } catch (Exception e) {
            execution = new AppointExecution(bookId, AppointStateEnum.INNER_ERROR);
        }

        return new Result<>(true, execution);
    }

}
